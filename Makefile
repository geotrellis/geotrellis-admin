js_setup:
	cd static_build && npm install
	cd static_build && webpack
	./sbt client fullOptJS

scala_setup:
	./sbt server assembly

build: scala_setup
	docker build -t moradology/geotrellis-admin:latest -f deploy/geotrellis-admin.dockerfile .

clean:
	rm -rf static_build/assets
	rm -rf static_build/node_modules
