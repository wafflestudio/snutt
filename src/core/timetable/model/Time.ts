export interface Time {
  subtract(other: Time): Time;

  addMinute(minute: number);

  addHour(hour: number);

  subtractMinute(minute: number);

  subtractHour(hour: number);

  toHourMinuteFormat();

  getHour(): number;

  getMinute(): number;

  getDecimalHour(): number;
}

export class Time implements Time {
  minute: number;

  constructor(minute: number) {
    this.minute = minute
  }

  static fromHourMinuteString(hourMinuteString: string): Time {
    const hourMinute = hourMinuteString.split(":")
        .map((hourOrMinute) => parseInt(hourOrMinute, 10));
    if (hourMinute.length !== 2) throw new HourMinuteFormatException();
    const [hour, minute] = hourMinute;
    const isInvalidHour = hour < 0 || hour > 23;
    const isInvalidMinute = minute < 0 || minute > 59;
    const containsNaN = Number.isNaN(hour) || Number.isNaN(minute)
    if (isInvalidHour || isInvalidMinute || containsNaN) throw new HourMinuteFormatException();
    const totalMinute = hour * 60 + minute;
    return new Time(totalMinute);
  }

  subtract(other: Time): Time {
    return new Time(this.minute - other.minute)
  }

  addMinute(minute: number) {
    return new Time(this.minute + minute)
  }

  addHour(hour: number) {
    return new Time(this.minute + hour * 60)
  }

  subtractMinute(minute: number) {
    return new Time(this.minute - minute)
  }

  subtractHour(hour: number) {
    return new Time(this.minute - hour * 60)
  }

  toHourMinuteFormat() {
    return `${String(this.getHour()).padStart(2, '0')}:${String(this.getMinute()).padStart(2, '0')}`
  }

  getHour(): number {
    return Math.trunc(this.minute / 60)
  }

  getMinute(): number {
    return Math.trunc(this.minute % 60)
  }

  getDecimalHour(): number {
    return this.minute / 60
  }

}

export class HourMinuteFormatException {
}
