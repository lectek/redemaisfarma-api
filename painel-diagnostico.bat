@echo off
chcp 65001 >nul
cls

:: ======= VARIÁVEIS =======
set "PROJETO=C:\dev\redemaisfarma-api"
set "LOG_DIR=%PROJETO%\logs"
set "LOG_DETALHADO=%LOG_DIR%\compilacao_completa.log"
set "LOG_FILTRADOS=%LOG_DIR%\erros_filtrados.log"
set "LOG_LOCAIS=%LOG_DIR%\locais_sugestao.log"

:: Cria a pasta logs se não existir
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

:: ======= MENU =======
:menu
cls
echo.
echo ================================
echo     DIAGNÓSTICO REDEMAISFARMA
echo ================================
echo.
echo  [1] Levantar containers Docker
echo  [2] Parar containers Docker
echo  [3] Build da aplicação COM testes
echo  [4] Build da aplicação SEM testes
echo  [5] Ver erros no Notepad
echo  [6] Abrir erro no VS Code
echo  [0] Sair
echo.
set /p opcao="Escolha uma opção: "

if "%opcao%"=="1" goto dockerUp
if "%opcao%"=="2" goto dockerDown
if "%opcao%"=="3" goto comTestes
if "%opcao%"=="4" goto semTestes
if "%opcao%"=="5" goto abrirNotepad
if "%opcao%"=="6" goto abrirVSCode
if "%opcao%"=="0" exit
goto menu

:: ======= OPÇÃO 1: LEVANTAR DOCKER =======
:dockerUp
cls
echo 🚀 Levantando containers via Docker Compose...
cd /d "%PROJETO%"
docker-compose up --build -d
echo.
pause
goto menu

:: ======= OPÇÃO 2: PARAR DOCKER =======
:dockerDown
cls
echo 🛑 Parando e removendo containers Docker...
cd /d "%PROJETO%"
docker-compose down
echo.
pause
goto menu

:: ======= OPÇÃO 3: COM TESTES =======
:comTestes
cls
echo 🔎 Rodando mvn clean install (com testes)...
cd /d "%PROJETO%"
call mvn clean install -e -X > "%LOG_DETALHADO%" 2>&1
goto processarErros

:: ======= OPÇÃO 4: SEM TESTES =======
:semTestes
cls
echo 🔧 Rodando mvn clean install -DskipTests...
cd /d "%PROJETO%"
call mvn clean install -DskipTests -e -X > "%LOG_DETALHADO%" 2>&1
goto processarErros

:: ======= ANÁLISE DE ERROS =======
:processarErros
findstr /I /C:"ERROR" "%LOG_DETALHADO%" > "%LOG_FILTRADOS%"
findstr /R "\.java:[0-9]*:" "%LOG_DETALHADO%" > "%LOG_LOCAIS%"

echo.
echo ===== RESULTADO DA COMPILAÇÃO =====
echo.

if exist "%LOG_FILTRADOS%" (
    echo ⚠️ ERROS DETECTADOS:
    type "%LOG_FILTRADOS%"
) else (
    echo ✅ Nenhum erro encontrado.
)

echo.
if exist "%LOG_LOCAIS%" (
    echo 📍 POSSÍVEIS LOCAIS DOS ERROS:
    type "%LOG_LOCAIS%"
) else (
    echo ℹ️ Nenhum arquivo Java com linha de erro detectado.
)

echo.
echo 📁 Logs completos salvos em: %LOG_DIR%
pause
goto menu

:: ======= ABRIR LOG NO NOTEPAD =======
:abrirNotepad
start notepad "%LOG_DETALHADO%"
goto menu

:: ======= ABRIR ARQUIVO NO VS CODE =======
:abrirVSCode
for /f "tokens=1 delims=:" %%i in (%LOG_LOCAIS%) do (
    set "ARQUIVO=%%i"
    goto abrirArquivoVSCode
)
echo ❌ Nenhum arquivo detectado.
pause
goto menu

:abrirArquivoVSCode
echo Abrindo "%ARQUIVO%" no VS Code...
code "%ARQUIVO%"
goto menu
