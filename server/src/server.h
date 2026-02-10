#ifndef SERVER_H
#define SERVER_H

#include <stdint.h>
#include <stdbool.h>

#define SERVER_DEFAULT_PORT 8443
#define SERVER_DEFAULT_BACKLOG 128
#define SERVER_MAX_CLIENTS 10000
#define SERVER_BUFFER_SIZE 65536

typedef struct server_context server_context_t;

typedef struct {
    const char* bind_address;
    uint16_t port;
    int backlog;
    const char* database_path;
    const char* cert_path;
    const char* key_path;
    bool use_tls;
    int log_level;
} server_config_t;

server_context_t* server_create(const server_config_t* config);
void server_destroy(server_context_t* server);
int server_run(server_context_t* server);
void server_stop(server_context_t* server);

#endif
