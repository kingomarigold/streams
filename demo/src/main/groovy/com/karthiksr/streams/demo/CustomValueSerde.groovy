package com.karthiksr.streams.demo

import org.apache.kafka.common.serialization.Serdes.WrapperSerde

class CustomValueSerde extends WrapperSerde<CustomValue>{

	CustomValueSerde() {
		
	}
	
	CustomValueSerde(serializer,deserializer) {
		super(serializer,deserializer)
	}
	
}
