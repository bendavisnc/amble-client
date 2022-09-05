
rundevserver: styles openapi clean
	npx shadow-cljs watch amble-client

openapi:
	mkdir -p resources/public/json
	cd ../amble-openapi; make clean; make openapi
	cp ../amble-openapi/target/openapi/openapi.json resources/public/json/openapi.json
	
styles:
	lessc resources/public/less/amble.less resources/public/css/amble.css


clean:
	rm -rf target	
