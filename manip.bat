@echo off
if [%1] == [] (
    echo Bad args, pass in the cave
    SET /p exit=Press enter to exit
) else (
    python videodigits.py find
    seed chresult
    seed caveviewer %1
    seed timer %1
)