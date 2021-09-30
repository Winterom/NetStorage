package app;

import message.Command;

public interface NetCallback {
    void call(Command command);
}
