package serilogj.core.pipeline;

import java.util.concurrent.ConcurrentHashMap;
import serilogj.core.*;
import serilogj.events.*;

// Copyright 2013-2015 Serilog Contributors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
public class MessageTemplateCache implements IMessageTemplateParser {
	private IMessageTemplateParser innerParser;
	private ConcurrentHashMap<String, MessageTemplate> templates = new ConcurrentHashMap<String, MessageTemplate>();

	private static final int MaxCacheItems = 1000;
	private static final int MaxCachedTemplateLength = 1024;

	public MessageTemplateCache(IMessageTemplateParser innerParser) {
		if (innerParser == null) {
			throw new IllegalArgumentException("innerParser");
		}
		this.innerParser = innerParser;
	}

	public MessageTemplate parse(String messageTemplate) {
		if (messageTemplate == null) {
			throw new IllegalArgumentException("messageTemplate");
		}

		if (messageTemplate.length() > MaxCachedTemplateLength) {
			return innerParser.parse(messageTemplate);
		}

		MessageTemplate result = templates.get(messageTemplate);
		if (result != null) {
			return result;
		}

		result = innerParser.parse(messageTemplate);

		if (templates.size() >= MaxCacheItems) {
			templates.clear();
		}

		templates.putIfAbsent(messageTemplate, result);
		return templates.get(messageTemplate);
	}
}
