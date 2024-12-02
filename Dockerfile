ARG RUST_VERSION=1.50.0

FROM rust:$RUST_VERSION
ARG UVM_VERSION=2.2.0

ENV RUST_BACKTRACE=1
ENV RUST_LOG="warning, uvm_core=trace, uvm_jni=trace"
ENV IN_DOCKER="1"


RUN curl -Lo "unity-version-manager-$UVM_VERSION.tar.gz" "https://github.com/Larusso/unity-version-manager/archive/v$UVM_VERSION.tar.gz"
RUN tar -xzf "unity-version-manager-$UVM_VERSION.tar.gz" && rm "unity-version-manager-$UVM_VERSION.tar.gz"

RUN cd "unity-version-manager-$UVM_VERSION" && PATH="${HOME}/.cargo/bin:$PATH" make install

FROM openjdk:11-jdk-buster
ARG USER_ID=1001
ARG GROUP_ID=100

RUN useradd -u ${USER_ID} -g ${GROUP_ID} --create-home jenkins_agent

USER jenkins_agent
COPY --from=0 /usr/local/bin/uvm* ./usr/local/bin/

RUN uvm install 2019.1.0a7 /home/jenkins_agent/.local/share/Unity-2019.1.0a7
