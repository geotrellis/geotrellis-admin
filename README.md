# Geotrellis Admin

This project includes tools useful for viewing catalogs of Geotrellis
data stored on S3. It is currently compatible with geotrellis metadata
from the 0.10 release or better.

## Usage

Add the S3 bucket and s3 key fields to `docker-compose.yml` and
call `docker-compose up`. At this point, you should be able to visit
localhost on port 8090.

If you're having trouble with AWS permissions, check to see that your
credentials are stored in the standard location (`~/.aws`).

