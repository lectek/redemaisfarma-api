/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.fasterxml.jackson.annotation.JsonInclude
 *  com.fasterxml.jackson.annotation.JsonInclude$Include
 *  com.fasterxml.jackson.annotation.JsonProperty
 *  jakarta.validation.constraints.NotBlank
 *  jakarta.validation.constraints.Size
 */
package br.com.redemaisfarma.adapters.inbound.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@JsonInclude(value=JsonInclude.Include.NON_NULL)
public record ImageGenRequestDTO(@NotBlank @Size(max=64) @JsonProperty(value="preset") @NotBlank @Size(max=64) String preset, @Size(max=800) @JsonProperty(value="prompt") @Size(max=800) String prompt, @JsonProperty(value="vars") Map<String, Object> vars, @Size(max=2048) @JsonProperty(value="inputImageUrl") @Size(max=2048) String inputImageUrl, @JsonProperty(value="removeBackground") boolean removeBackground, @JsonProperty(value="upscale") boolean upscale) {
    public List<String> cores() {
        if (this.vars == null) {
            return List.of();
        }
        Object v = this.vars.get("cores");
        if (v instanceof List) {
            List list = (List)v;
            return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }
        return List.of();
    }

    public String varText(String key, String def) {
        String s;
        if (this.vars == null) {
            return def;
        }
        Object v = this.vars.get(key);
        return v instanceof String && !(s = (String)v).isBlank() ? s : def;
    }

    public static enum Preset {
        packshot,
        banner_feed,
        banner_story,
        carrossel;


        public static boolean isValid(String p) {
            try {
                Preset.valueOf(p);
                return true;
            }
            catch (Exception e) {
                return false;
            }
        }
    }
}

