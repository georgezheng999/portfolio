// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public final class FindMeetingQuery {

  private boolean eventHasConflict(Event e, MeetingRequest request) {
    final Set<String> eventAttendees = e.getAttendees();
    for (final String reqAttendee : request.getAttendees()) {
      if (eventAttendees.contains(reqAttendee)) {
        return true;
      }
    }
    return false;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> conflictTimes = new ArrayList<>();
    conflictTimes.add(TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0)); 
    for (final Event e : events) {
      if (eventHasConflict(e, request)) {
        conflictTimes.add(e.getWhen());
      }      
    }
    Collections.sort(conflictTimes, conflictTimes.ORDER_BY_START);
    final Collection<TimeRange> queryResult = new ArrayList<>();
    final int requestDuration = request.getDuration();
    for (int i = 1; i < conflictTimes.size(); i++) {
      final int durationDiff = conflictTimes.get(i).start() - conflictTimes.get(i - 1).end(); 
      if (durationDiff >= requestDuration) {
        queryResult.add(TimeRange.fromStartDuration(conflictTimes.get(i - 1).end(), durationDiff));
      }
    }
    final int lastEventEndTime = conflictTimes.get(conflictTimes.size() - 1).end();
    if (TimeRange.END_OF_DAY - lastEventEndTime >= requestDuration) {
      queryResult.add(TimeRange.fromStartEnd(lastEventEndTime, TimeRange.END_OF_DAY, true));
    }
    return queryResult;
  }
}
