#include "server.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

struct server_context {
    server_config_t config;
    int running;
};

server_context_t* server_create(const server_config_t* config) {
    if (!config) {
        return NULL;
    }
    
    server_context_t* server = calloc(1, sizeof(server_context_t));
    if (!server) {
        return NULL;
    }
    
    memcpy(&server->config, config, sizeof(server_config_t));
    server->running = 0;
    
    return server;
}

void server_destroy(server_context_t* server) {
    if (server) {
        free(server);
    }
}

int server_run(server_context_t* server) {
    if (!server) {
        return -1;
    }
    
    server->running = 1;
    
    printf("SecureChat Server starting on %s:%d\n", 
           server->config.bind_address ? server->config.bind_address : "0.0.0.0",
           server->config.port);
    
    printf("Server ready (press Ctrl+C to stop)\n");
    
    return 0;
}

void server_stop(server_context_t* server) {
    if (server) {
        server->running = 0;
    }
}
