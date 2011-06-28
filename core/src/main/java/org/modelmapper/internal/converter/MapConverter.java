/**
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modelmapper.internal.converter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.modelmapper.internal.util.TypeResolver;
import org.modelmapper.spi.Mapping;
import org.modelmapper.spi.MappingContext;
import org.modelmapper.spi.PropertyInfo;
import org.modelmapper.spi.PropertyMapping;

/**
 * Converts {@link Map} instances to each other.
 * 
 * @author Jonathan Halterman
 */
class MapConverter extends AbstractConditionalConverter<Map<?, ?>, Map<Object, Object>> {
  @Override
  public Map<Object, Object> convert(MappingContext<Map<?, ?>, Map<Object, Object>> context) {
    Map<?, ?> source = context.getSource();
    Map<Object, Object> destination = context.getDestination() == null ? createDestination(context)
        : context.getDestination();
    Mapping mapping = context.getMapping();

    Class<?> keyElementType = Object.class;
    Class<?> valueElementType = Object.class;
    if (mapping != null && mapping instanceof PropertyMapping) {
      PropertyInfo destInfo = ((PropertyMapping) mapping).getLastDestinationProperty();
      Class<?>[] elementTypes = TypeResolver.resolveArguments(destInfo.getGenericType(), destInfo
          .getMember().getDeclaringClass());
      if (elementTypes != null) {
        keyElementType = elementTypes[0];
        valueElementType = elementTypes[1];
      }
    }

    for (Entry<?, ?> entry : source.entrySet()) {
      MappingContext<?, ?> keyContext = context.create(entry.getKey(), keyElementType);
      MappingContext<?, ?> valueContext = context.create(entry.getValue(), valueElementType);
      Object key = context.getMappingEngine().map(keyContext);
      Object value = context.getMappingEngine().map(valueContext);
      destination.put(key, value);
    }

    return destination;
  }

  @Override
  public boolean supports(Class<?> sourceType, Class<?> destinationType) {
    return Map.class.isAssignableFrom(sourceType) && Map.class.isAssignableFrom(destinationType);
  }

  protected Map<Object, Object> createDestination(
      MappingContext<Map<?, ?>, Map<Object, Object>> context) {
    if (context.getDestinationType().isInterface())
      return new HashMap<Object, Object>();

    return context.getMappingEngine().createDestination(context);
  }
}
