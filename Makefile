figdev: lesscompile
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