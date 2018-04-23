package com.karthiksr.streams.demo.storm

import org.apache.storm.tuple.Tuple
import org.apache.storm.windowing.TimestampExtractor

class StormTimestampExtractor implements TimestampExtractor {

	@Override
	public long extractTimestamp(Tuple tuple) {
		tuple.getLongByField('timeStamp')
	}

}
