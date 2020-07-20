package com.google.sps;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

public final class FindMeetingQuery {

  // Returns whether or not any mandatory or optional attendees of this request have a conflict with the event.
  // Setting considerOpt to true toggles the consideration of optional attendees
  private boolean eventHasConflict(Event e, MeetingRequest request, boolean considerOpt) {
    final Collection<String> mandAttendees = request.getAttendees();
    final Collection<String> optAttendees = request.getOptionalAttendees();
    for (final String eventAttendee : e.getAttendees()) {
      if (mandAttendees.contains(eventAttendee)) {
        return true;
      }
      if (considerOpt && optAttendees.contains(eventAttendee)) {
        return true;
      } 
    }
    return false;
  }

  // Given a list conflictTimes containing times that are not open and a request,
  // returns a complementary list of open times that are at least as long as the requested duration
  public Collection<TimeRange> getOpenTimes(List<TimeRange> conflictTimes, MeetingRequest request) {
    Collections.sort(conflictTimes, TimeRange.ORDER_BY_START);
    final Collection<TimeRange> queryResult = new ArrayList<>();
    final int requestDuration = (int) request.getDuration();
    int maxEndingTimeSoFar = conflictTimes.get(0).end();
    for (int i = 1; i < conflictTimes.size(); i++) {
      final int durationDiff = conflictTimes.get(i).start() - maxEndingTimeSoFar; 
      if (durationDiff >= requestDuration) {
        queryResult.add(TimeRange.fromStartDuration(conflictTimes.get(i - 1).end(), durationDiff));
        queryResult.add(TimeRange.fromStartDuration(maxEndingTimeSoFar, durationDiff));
      }
      maxEndingTimeSoFar = max(maxEndingTimeSoFar, conflictTimes.get(i).end());
      maxEndingTimeSoFar = Math.max(maxEndingTimeSoFar, conflictTimes.get(i).end());
    }
    if (TimeRange.END_OF_DAY - maxEndingTimeSoFar >= requestDuration) {
      queryResult.add(TimeRange.fromStartEnd(maxEndingTimeSoFar, TimeRange.END_OF_DAY, true));
    }
    return queryResult;
  }

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    List<TimeRange> mandConflictTimes = new ArrayList<>();
    List<TimeRange> mandAndOptConflictTimes = new ArrayList<>();
    mandConflictTimes.add(TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0)); 
    mandAndOptConflictTimes.add(TimeRange.fromStartDuration(TimeRange.START_OF_DAY, 0)); 
    for (final Event e : events) {
      if (eventHasConflict(e, request, false)) {
        mandConflictTimes.add(e.getWhen());
      } 
      if (eventHasConflict(e, request, true)) {
        mandAndOptConflictTimes.add(e.getWhen());
      }
    }
    final Collection<TimeRange> mandatoryUserTimes = getOpenTimes(mandConflictTimes, request);
    final Collection<TimeRange> mandAndOptUserTimes = getOpenTimes(mandAndOptConflictTimes, request);
    return (mandAndOptUserTimes.isEmpty()) ? mandatoryUserTimes : mandAndOptUserTimes; 
    return (mandAndOptUserTimes.isEmpty() && !request.getAttendees().isEmpty()) ? mandatoryUserTimes : mandAndOptUserTimes; 
  }
}