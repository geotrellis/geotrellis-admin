# Geotrellis Admin

This project includes tools useful for viewing catalogs of Geotrellis
data stored on S3. It is currently compatible with geotrellis metadata
from the 0.10 release or better.

## Dependencies

- [npm](https://www.npmjs.com/)
- [webpack](https://webpack.github.io/)
- [docker](https://www.docker.com/)
- [docker-compose](https://docs.docker.com/compose/overview/)

All Scala dependencies (geotrellis, spark, etc.) are pulled through `sbt`
in the usual way.

It is also assumed that you have a valid S3 bucket with ingested Geotrellis data.

## Building

Run `make build` in the project directory. This will compile all project code
and assets, and create the docker images used to run the client.

## Usage

Complete the `S3_BUCKET` and `S3_KEY` fields in `docker-compose.yml` and
call `docker-compose up`. At this point, you should be able to visit
localhost on port 8090 to access the viewing client.

**Note**: If you're having trouble with AWS permissions, check to see
that your credentials are stored in the standard location (`~/.aws`).

