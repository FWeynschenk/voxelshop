#ifndef CONNECTING_STATE_H
#define CONNECTING_STATE_H

#include <utils\ssm.h>
#include <game\game.h>
#include <network\fos_socket.h>
#include "game_connected_state.h"
#include "game_disconnected_state.h"

class connecting_state : public simple_state {
private:
	shared_fos_socket _shared_socket;
	uint64 _startTime;
public:
	void on_enter() {
		// start the timer
		_startTime = s3eTimerGetMs();
		// create the socket
		_shared_socket = shared_fos_socket(new fos_socket());
		// try to connect
		_shared_socket->connect("127.0.0.1", 7633);
	}
	void update(uint64 dt) {
		if(_shared_socket->is_connected()) {
			// connected
			_sm->switch_state(new connected_state(_shared_socket));
		} else {
			// check for timeout or error
			if(s3eTimerGetMs() - _startTime > SOCKET_TIMEOUT || _shared_socket->is_errors()) {
				// disconnected
				_sm->switch_state(new disconnected_state(_shared_socket->is_errors()));
			}
		}
	}
	void on_exit() {
	}
};

#endif