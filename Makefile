figdev: openapi lesscompile
	@echo "Running main dev build..."
	clj -M:dev -b dev -r

fighelp:
	@echo "Showing fig help..."
	clj -M:dev --help

format:
	@echo "Formatting cljs..."
	standard-clj fix src

resources/public/css/style.css: less/amble.less
	lessc less/amble.less resources/public/css/style.css

lesscompile: resources/public/css/style.css
	@echo Compiling css resource from less source.

resources/public/json/openapi.json:
	mkdir -p resources/public/json
	cd ../amble-openapi; make clean; make openapi	
	cp ../amble-openapi/target/openapi/openapi.json resources/public/json/openapi.json
	
openapi: resources/public/json/openapi.json
	@echo Provisioning openapi spec.

clean: cleancss
	@echo "Cleaning up..."
	rm -rf resources/public/json/openapi.json
	rm -rf target
	rm -rf node_modules

cleancss:
	@echo "Removing compiled css..."
	rm -rf resources/public/css

gitadd:
	git add ':!.gitignore' -u
