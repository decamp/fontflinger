#ifndef TTC2TTF_H
#define TTC2TTF_H

#include <netinet/in.h>

typedef unsigned int uint32;
typedef unsigned char uchar;
typedef unsigned short uint16;

#define B_BENDIAN_TO_HOST_INT32(x) ntohl(x)
#define B_BENDIAN_TO_HOST_INT16(x) ntohs(x)
#define B_HOST_TO_BENDIAN_INT32(x) htonl(x)
#define B_HOST_TO_BENDIAN_INT16(x) htons(x)

#endif