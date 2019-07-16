FROM openjdk:8-jdk


RUN mkdir -p /home/ci

# Create an app user so our program doesn't run as root.
RUN groupadd -r ci &&\
    useradd -r -g ci -d /home/ci -s /sbin/nologin -c "Docker image user" ci

# Set the home directory to our app user's home.
ENV HOME=/home/ci
ENV RUST_BACKTRACE=1
ENV RUST_LOG="warning, uvm_core=trace, uvm_jni=trace"
ENV IN_DOCKER="1"

RUN apt-get update
RUN apt-get install -y  build-essential libssl-dev pkg-config openssl p7zip-full cpio -y

RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh -s -- -y

ENV PATH="${HOME}/.cargo/bin:${PATH}"

WORKDIR /home/ci/

RUN curl -Lo unity-version-manager-2.2.0.tar.gz https://github.com/Larusso/unity-version-manager/archive/v2.2.0.tar.gz && \
    tar -xzf unity-version-manager-2.2.0.tar.gz && \
    cd unity-version-manager-2.2.0 && make install && \
    uvm install 2019.1.0a7 /home/ci/.local/share/Unity-2019.1.0a7

# Chown all the files to the app user.
RUN chown -R ci:ci $HOME
RUN chmod -R 777 $HOME