figdev:
	@echo "Running main dev build..."
	clj -M:dev -b dev -r


fighelp:
	@echo "Showing fig help..."
	clj -M:dev --help

format:
	@echo "Formatting cljs..."
	standard-clj fix src
