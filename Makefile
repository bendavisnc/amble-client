
# Auto builds project and runs dev server.
rundevserver: installnpmdeps styles openapi clean
	npx shadow-cljs watch frontend


openapi:
	mkdir -p resources/public/json
	cd ../amble-openapi; make clean; make openapi
	cp ../amble-openapi/target/openapi/openapi.json resources/public/json/openapi.json
	
styles:
	lessc resources/public/less/amble.less resources/public/css/amble.css
	
watchstyles:
	find . -name '*.less' | entr make styles


clean:
	rm -rf target	
	rm -rf public/js/ambleout
	
	
installnpmdeps:
	npm install

