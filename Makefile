shaddev: shaddevbase 
	@echo "Running main dev build..."

shaddevbase: openapi lesscompile webpack npminstall csslibs
	@echo "..."
	npx shadow-cljs watch app

shadhelp:
	@echo "Showing shadowcljs help..."
	clj -M:dev --help

format:
	@echo "Formatting cljs..."
	standard-clj fix src

LESS_FILES := $(shell find less -name '*.less')

resources/public/css/style.css: $(LESS_FILES)
	lessc less/amble.less resources/public/css/style.css

lesscompile: resources/public/css/style.css
	@echo Compiling css resource from less source.

lesscompile-watch:
	find less -name "*.less" | entr -c make lesscompile

resources/public/css/noty.css:
	cp node_modules/noty/lib/noty.css resources/public/css/noty.css

csslibs: notycss animatecss

notycss: resources/public/css/noty.css

resources/public/css/animate.css:
	cp node_modules/animate.css/animate.css resources/public/css/animate.css

animatecss: resources/public/css/animate.css

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
	rm -f package-lock.json 


cleancss:
	@echo "Removing compiled css..."
	rm -rf resources/public/css

gitadd:
	git add ':!.gitignore' -u

npminstall:
	npm install 


webpack:
	npm install -D webpack-cli


lessformat:
	npx stylelint "**/*.less" --fix