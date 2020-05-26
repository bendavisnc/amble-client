
openapi:
	mkdir -p resources/public/json
	cd ../amble-openapi; make clean; make openapi
	cp ../amble-openapi/target/openapi/openapi.json resources/public/json/openapi.json