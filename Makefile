
# Auto builds project and runs dev server.
rundevserver: installnpmdeps styles openapi clean
	npx shadow-cljs watch frontend


openapi:
	echo Provisioning openapi spec.
	mkdir -p resources/public/json
	cd ../amble-openapi; make clean; make openapi
	cp ../amble-openapi/target/openapi/openapi.json resources/public/json/openapi.json
	
styles_less:
	lessc resources/public/less/amble.less resources/public/css/amble.css
	
styles_node:
	cp node_modules/ag-grid-community/styles/ag-grid.css resources/public/css
	cp node_modules/ag-grid-community/styles/ag-theme-alpine.css resources/public/css
	
styles: styles_less styles_node
	echo Copying stylesheets.
	
watchstyles:
	find . -name '*.less' | entr make styles

clean:
	rm -rf target	
	rm -rf public/js/ambleout
	
	
installnpmdeps:
	npm install

