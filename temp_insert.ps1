$path='boot-app/src/main/java/br/com/redemaisfarma/adapters/outbound/persistence/entity/ClienteEntity.java'
$content=Get-Content -Raw -Encoding UTF8 $path
$needle='telefone;'
$idx=$content.IndexOf($needle)
if ($idx -lt 0) { throw 'needle not found' }
$insertPos=$idx + $needle.Length
$insert="\r\n    @Size(max=512, message=\"A URL da foto pode ter até 512 caracteres\")\r\n    @Column(name=\"foto_url\", length=512)\r\n    private String fotoUrl;"
$content=$content.Insert($insertPos,$insert)
Set-Content -Encoding UTF8 -Path $path -Value $content
