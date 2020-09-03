@echo off 
    setlocal enableextensions disabledelayedexpansion

    set "search=4 	# type"
    set "replace=2 	# type"

    set "textFile=texts.szs"

    for /f "delims=" %%i in ('type "%textFile%" ^& break ^> "%textFile%" ') do (
        set "line=%%i"
        setlocal enabledelayedexpansion
        >>"%textFile%" echo(!line:%search%=%replace%!
        endlocal
    )