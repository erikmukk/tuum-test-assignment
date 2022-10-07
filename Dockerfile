FROM postgres:13
RUN localedef -i et_EE -c -f UTF-8 -A /usr/share/locale/locale.alias et_EE.UTF-8
ENV LANG et_EE.utf8
COPY src/main/resources/_dbinit/__dbcreate.sql /docker-entrypoint-initdb.d/
