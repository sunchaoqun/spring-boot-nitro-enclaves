cd ~/spring-boot-nitro-enclaves/examples/simple-echo/simple-echo-enclave/enclave-build

mkdir bin

./generate_eif.sh

docker build --network=host -t enclave-w .

Test:
docker run -ti -v /var/run/docker.sock:/var/run/docker.sock --device=/dev/nitro_enclaves:/dev/nitro_enclaves:rw enclave-w /bin/bash


nitro-cli terminate-enclave --all