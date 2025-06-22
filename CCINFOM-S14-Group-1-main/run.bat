REM This script builds the project into the bin directory, copying over
REM any dependencies as well.

REM WARNING: If, for some reason, the bin folder of the current working
REM directory includes any important document, then please move it, as this
REM script deletes the bin folder in the current working directory.

REM Delete bin folder and compile with library dependencies
del /s /q bin
javac -cp .;lib/* src/controller/Driver.java -d bin/

REM Make bin folder directories
mkdir bin
mkdir bin\src\model
mkdir bin\src\view
mkdir bin\src\view\gui\records
mkdir bin\src\view\gui\reports
mkdir bin\src\view\gui\transactions
mkdir bin\src\view\widget
mkdir bin\src\util
mkdir bin\src\controller

REM Copy JSON files used in the GUI's Subtab windows
copy src\view\gui\records\*.json bin\src\view\gui\records\
copy src\view\gui\reports\*.json bin\src\view\gui\reports\
copy src\view\gui\transactions\*.json bin\src\view\gui\transactions\

REM Go to bin directory, then execute using java with libraries and password
cd bin
java -cp .;../lib/* src/controller/Driver  < ../password.in

REM Go back to original directory
cd ..