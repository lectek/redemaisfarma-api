package br.com.redemaisfarma.application.service.delivery;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DeliveryRouteService {

    private static final Logger log = LoggerFactory.getLogger(
            DeliveryRouteService.class
    );

    private static final int DISTANCE_SCALE = 2;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String nominatimBaseUrl;
    private final String userAgent;
    private final Duration timeout;

    public DeliveryRouteService(
            final ObjectMapper objectMapperValue,
            @Value("${app.route.nominatim.base-url:https://nominatim.openstreetmap.org/search}")
            final String nominatimBaseUrlValue,
            @Value("${app.route.nominatim.user-agent:RedeMaisFarma/1.0}")
            final String userAgentValue,
            @Value("${app.route.nominatim.timeout-ms:6000}")
            final long timeoutMs
    ) {
        this.objectMapper = objectMapperValue;
        this.nominatimBaseUrl = nominatimBaseUrlValue;
        this.userAgent = userAgentValue;
        this.timeout = Duration.ofMillis(Math.max(timeoutMs, 1000L));
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(this.timeout)
                .build();
    }

    public PlannedRoute plan(
            final String origemEndereco,
            final List<DeliveryStopInput> stops
    ) {
        if (stops == null || stops.isEmpty()) {
            throw new IllegalArgumentException(
                    "Informe ao menos um pedido para roteirizacao."
            );
        }

        final String origemNormalizada = normalizeAddress(origemEndereco);
        if (origemNormalizada.isBlank()) {
            throw new IllegalArgumentException(
                    "Endereco de origem nao informado."
            );
        }

        final Map<String, GeoPoint> cache = new HashMap<>();
        final GeoPoint origem = geocodeCached(origemNormalizada, cache);
        final List<ResolvedStop> resolved = new ArrayList<>(stops.size());
        for (DeliveryStopInput stop : stops) {
            if (stop == null || stop.pedidoId() == null) {
                throw new IllegalArgumentException("Pedido invalido na rota.");
            }
            final String endereco = normalizeAddress(stop.enderecoEntrega());
            if (endereco.isBlank()) {
                throw new IllegalArgumentException(
                        "Pedido #" + stop.pedidoId()
                        + " sem endereco de entrega."
                );
            }
            resolved.add(new ResolvedStop(stop, geocodeCached(endereco, cache)));
        }

        final BestRoute best = optimize(origem, resolved);
        final List<DeliveryStopPlan> plans = new ArrayList<>(resolved.size());
        final List<GeoPoint> orderedPoints = new ArrayList<>(resolved.size());

        double acumulada = 0.0d;
        GeoPoint anterior = origem;
        for (int i = 0; i < best.order.length; i++) {
            final ResolvedStop stop = resolved.get(best.order[i]);
            final double trecho = distanceKm(anterior, stop.point());
            acumulada += trecho;
            orderedPoints.add(stop.point());
            plans.add(
                    new DeliveryStopPlan(
                            i + 1,
                            stop.input().pedidoId(),
                            defaultString(stop.input().clienteNome(), "Cliente"),
                            stop.input().enderecoEntrega(),
                            stop.input().codigoEntrega(),
                            stop.input().status(),
                            toScale(trecho),
                            toScale(acumulada),
                            stop.point().lat(),
                            stop.point().lon()
                    )
            );
            anterior = stop.point();
        }

        final String mapsUrl = buildGoogleMapsUrl(origem, orderedPoints);
        return new PlannedRoute(
                origemNormalizada,
                toScale(best.distance),
                plans,
                mapsUrl
        );
    }

    private GeoPoint geocodeCached(
            final String endereco,
            final Map<String, GeoPoint> cache
    ) {
        return cache.computeIfAbsent(endereco, this::geocode);
    }

    private GeoPoint geocode(final String endereco) {
        try {
            final String query = URLEncoder.encode(
                    endereco,
                    StandardCharsets.UTF_8
            );
            final URI uri = URI.create(
                    nominatimBaseUrl
                    + "?format=json&countrycodes=br&limit=1&q="
                    + query
            );
            final HttpRequest request = HttpRequest.newBuilder(uri)
                    .header("Accept", "application/json")
                    .header("User-Agent", userAgent)
                    .timeout(timeout)
                    .GET()
                    .build();
            final HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() >= 400) {
                throw new IllegalStateException(
                        "Falha ao geocodificar endereco: HTTP "
                        + response.statusCode()
                );
            }
            return parseFirstPoint(response.body(), endereco);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(
                    "Geocodificacao interrompida para endereco: " + endereco,
                    ex
            );
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Falha de comunicacao na geocodificacao: " + endereco,
                    ex
            );
        } catch (RuntimeException ex) {
            log.warn("Falha ao geocodificar endereco '{}': {}", endereco,
                    ex.getMessage());
            throw ex;
        }
    }

    private GeoPoint parseFirstPoint(final String responseBody,
                                     final String endereco) throws IOException {
        final JsonNode root = objectMapper.readTree(responseBody);
        if (!root.isArray() || root.isEmpty()) {
            throw new IllegalStateException(
                    "Endereco nao encontrado para roteirizacao: " + endereco
            );
        }
        final JsonNode first = root.get(0);
        final double lat = parseCoordinate(first.path("lat").asText(""));
        final double lon = parseCoordinate(first.path("lon").asText(""));
        if (!Double.isFinite(lat) || !Double.isFinite(lon)) {
            throw new IllegalStateException(
                    "Coordenadas invalidas para endereco: " + endereco
            );
        }
        return new GeoPoint(lat, lon);
    }

    private static double parseCoordinate(final String raw) {
        if (raw == null || raw.isBlank()) {
            return Double.NaN;
        }
        try {
            return Double.parseDouble(raw.replace(',', '.'));
        } catch (NumberFormatException ex) {
            return Double.NaN;
        }
    }

    private static BestRoute optimize(
            final GeoPoint origem,
            final List<ResolvedStop> stops
    ) {
        final int size = stops.size();
        final BestRoute best = new BestRoute(size);
        final int[] order = new int[size];
        final boolean[] used = new boolean[size];
        dfs(origem, origem, stops, used, order, 0, 0.0d, best);
        return best;
    }

    private static void dfs(
            final GeoPoint origem,
            final GeoPoint anterior,
            final List<ResolvedStop> stops,
            final boolean[] used,
            final int[] order,
            final int depth,
            final double distanceSoFar,
            final BestRoute best
    ) {
        if (distanceSoFar >= best.distance) {
            return;
        }
        if (depth == stops.size()) {
            best.distance = distanceSoFar;
            best.order = order.clone();
            return;
        }

        for (int i = 0; i < stops.size(); i++) {
            if (used[i]) {
                continue;
            }
            used[i] = true;
            order[depth] = i;
            final GeoPoint current = stops.get(i).point();
            final GeoPoint from = depth == 0 ? origem : anterior;
            final double leg = distanceKm(from, current);
            dfs(
                    origem,
                    current,
                    stops,
                    used,
                    order,
                    depth + 1,
                    distanceSoFar + leg,
                    best
            );
            used[i] = false;
        }
    }

    private static double distanceKm(final GeoPoint a, final GeoPoint b) {
        final double earthRadiusKm = 6371.0088d;
        final double dLat = Math.toRadians(b.lat() - a.lat());
        final double dLon = Math.toRadians(b.lon() - a.lon());
        final double lat1 = Math.toRadians(a.lat());
        final double lat2 = Math.toRadians(b.lat());

        final double sinDlat = Math.sin(dLat / 2.0d);
        final double sinDlon = Math.sin(dLon / 2.0d);
        final double h = (sinDlat * sinDlat)
                + Math.cos(lat1) * Math.cos(lat2) * sinDlon * sinDlon;
        return 2.0d * earthRadiusKm * Math.asin(Math.sqrt(h));
    }

    private static String buildGoogleMapsUrl(
            final GeoPoint origin,
            final List<GeoPoint> orderedPoints
    ) {
        if (orderedPoints.isEmpty()) {
            return "";
        }

        final String originParam = formatCoordinates(origin);
        final String destination = formatCoordinates(
                orderedPoints.get(orderedPoints.size() - 1)
        );
        final StringBuilder url = new StringBuilder(
                "https://www.google.com/maps/dir/?api=1&travelmode=driving"
        );
        url.append("&origin=").append(encode(originParam));
        url.append("&destination=").append(encode(destination));

        if (orderedPoints.size() > 1) {
            final List<String> waypoints = new ArrayList<>();
            for (int i = 0; i < orderedPoints.size() - 1; i++) {
                waypoints.add(formatCoordinates(orderedPoints.get(i)));
            }
            url.append("&waypoints=").append(encode(String.join("|", waypoints)));
        }
        return url.toString();
    }

    private static String formatCoordinates(final GeoPoint point) {
        return String.format(
                Locale.US,
                "%.6f,%.6f",
                point.lat(),
                point.lon()
        );
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String normalizeAddress(final String value) {
        return value == null ? "" : value.trim();
    }

    private static String defaultString(final String value,
                                        final String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static BigDecimal toScale(final double value) {
        return BigDecimal.valueOf(value).setScale(
                DISTANCE_SCALE,
                RoundingMode.HALF_UP
        );
    }

    private record GeoPoint(double lat, double lon) {
    }

    private record ResolvedStop(
            DeliveryStopInput input,
            GeoPoint point
    ) {
    }

    private static final class BestRoute {
        private double distance = Double.POSITIVE_INFINITY;
        private int[] order;

        private BestRoute(final int size) {
            this.order = new int[size];
        }
    }

    public record DeliveryStopInput(
            Long pedidoId,
            String clienteNome,
            String enderecoEntrega,
            String codigoEntrega,
            String status
    ) {
    }

    public record DeliveryStopPlan(
            int ordem,
            Long pedidoId,
            String clienteNome,
            String enderecoEntrega,
            String codigoEntrega,
            String status,
            BigDecimal distanciaAnteriorKm,
            BigDecimal distanciaAcumuladaKm,
            double latitude,
            double longitude
    ) {
    }

    public record PlannedRoute(
            String origem,
            BigDecimal distanciaTotalKm,
            List<DeliveryStopPlan> paradas,
            String mapaUrl
    ) {
    }
}

