package com.frostedcorner.ai;

import com.frostedcorner.assistant.CustomerIntent;

public interface AiClient {

    CustomerIntent interpret(String message);
}