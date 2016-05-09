js_setup:
	cd static_build && npm install
	cd static_build && webpack
	./sbt "project client" fullOptJS

scala_setup:
	./sbt "project server" assembly

build_jvm: scala_setup
	docker build -t moradology/geotrellis-admin-server:latest -f deploy/geotrellis-admin-server.dockerfile .

build_js: js_setup
	docker build -t moradology/geotrellis-admin-client:latest -f deploy/geotrellis-admin-client.dockerfile .

build: build_js build_jvm

push: build
	docker push moradology/geotrellis-admin-client:latest
	docker push moradology/geotrellis-admin-server:latest

clean:
	./sbt clean
	rm -rf static_build/assets
	rm -rf static_build/node_modules
