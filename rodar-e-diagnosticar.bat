@echo off
chcp 65001 > nul
color 0A
cls

echo ================= DIAGNÓSTICO REDEMAISFARMA =================
echo.
echo [1] Tentar rodar API com testes
echo [2] Rodar API ignorando testes
echo [3] Ver último erro no Notepad
echo [4] Abrir erros do teste no VS Code
echo [0] Sair
echo.

set /p opcao=Escolha uma opção: 

if "%opcao%"=="1" goto COM_TESTES
if "%opcao%"=="2" goto SEM_TESTES
if "%opcao%"=="3" start notepad target\logs\erros_filtrados.log & goto FIM
if "%opcao%"=="4" code target\surefire-reports & goto FIM
if "%opcao%"=="0" exit

:COM_TESTES
echo.
echo ⏳ Compilando com testes...
mvn clean package > target\logs\erros.log 2>&1

findstr /i /c:"[ERROR]" target\logs\erros.log > target\logs\erros_filtrados.log

findstr /i /c:"Caused by" target\logs\erros.log > target\logs\locais_sugestao.log

if %errorlevel% equ 0 (
    echo ❌ Erros encontrados. Veja os arquivos de log em target\logs.
    goto FIM
)

echo ✅ Build finalizada com sucesso!
mvn spring-boot:run
goto FIM

:SEM_TESTES
echo.
echo ⏳ Compilando com testes ignorados...
mvn clean spring-boot:run -DskipTests
goto FIM

:FIM
echo.
pause
