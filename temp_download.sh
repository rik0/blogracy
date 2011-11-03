DOWNLOAD_DIR=~/Downloads
FILES="Vuze_4700.jar commons-cli.jar junit.jar log4j.jar"

for file in $FILES; do
	cp "$DOWNLOAD_DIR/$file" ./lib
done