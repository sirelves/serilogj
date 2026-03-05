package serilogj.policies;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import serilogj.core.ILogEventPropertyValueFactory;
import serilogj.core.IScalarConversionPolicy;
import serilogj.core.ScalarConversionPolicyResult;
import serilogj.events.ScalarValue;

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

public class EnumScalarConversionPolicy implements IScalarConversionPolicy {
	private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<Integer, ScalarValue>> _values = new ConcurrentHashMap<>();

	@Override
	@SuppressWarnings("rawtypes")
	public ScalarConversionPolicyResult tryConvertToScalar(Object value,
			ILogEventPropertyValueFactory propertyValueFactory) {
		ScalarConversionPolicyResult result = new ScalarConversionPolicyResult();
		if (!value.getClass().isEnum()) {
			return result;
		}

		result.isValid = true;
		ConcurrentHashMap<Integer, ScalarValue> enumValues = _values.computeIfAbsent(
				value.getClass(), k -> new ConcurrentHashMap<>());

		Integer enumOrdinal = ((Enum) value).ordinal();
		result.result = enumValues.computeIfAbsent(enumOrdinal, k -> new ScalarValue(value));
		return result;
	}
}
