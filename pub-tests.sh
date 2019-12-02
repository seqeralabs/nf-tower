#!/bin/bash
set -x
export AWS_ACCESS_KEY_ID=${NXF_AWS_ACCESS}
export AWS_SECRET_ACCESS_KEY=${NXF_AWS_SECRET}
export AWS_DEFAULT_REGION=eu-west-1

base=s3://www2.nextflow.io/tests/tower

# delete previous run
aws --region $AWS_DEFAULT_REGION s3 rm --only-show-errors --recursive $base

# upload unit test results
for x in $(find . -path \*build/reports/tests/test); do
aws --region $AWS_DEFAULT_REGION s3 sync $x $base/${x#./} \
 --only-show-errors \
 --cache-control max-age=0 \
 --metadata-directive REPLACE \
 --storage-class STANDARD \
 --acl public-read \
 --delete
done

