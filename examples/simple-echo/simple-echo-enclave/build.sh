nitro-cli terminate-enclave --all

mvn clean install

docker build /home/ec2-user/spring-boot-nitro-enclaves/examples/simple-echo/simple-echo-enclave -t simple-echo-enclave

nitro-cli build-enclave --docker-uri simple-echo-enclave:latest --output-file simple-echo-enclave.eif

nitro-cli run-enclave --eif-path simple-echo-enclave.eif --memory 8192 --cpu-count 4 --enclave-cid 4 --debug-mode --attach-console
