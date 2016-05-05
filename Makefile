js_setup: clean
	cd static_build && npm install
	cd static_build && webpack
	./sbt "project client" fullOptJS

scala_setup: clean
	./sbt "project server" assembly

build: js_setup scala_setup
	docker build -t moradology/geotrellis-admin-server:latest -f deploy/geotrellis-admin-server.dockerfile .
	docker build -t moradology/geotrellis-admin-client:latest -f deploy/geotrellis-admin-client.dockerfile .

clean:
	./sbt clean
	rm -rf static_build/assets
	rm -rf static_build/node_modules
