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
  totalMinute: number;

  constructor(minute: number) {
    this.totalMinute = minute
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
    return new Time(this.totalMinute - other.totalMinute)
  }

  addMinute(minute: number) {
    return new Time(this.totalMinute + minute)
  }

  addHour(hour: number) {
    return new Time(this.totalMinute + hour * 60)
  }

  subtractMinute(minute: number) {
    return new Time(this.totalMinute - minute)
  }

  subtractHour(hour: number) {
    return new Time(this.totalMinute - hour * 60)
  }

  toHourMinuteFormat() {
    return `${String(this.getHour()).padStart(2, '0')}:${String(this.getMinute()).padStart(2, '0')}`
  }

  getHour(): number {
    return Math.trunc(this.totalMinute / 60)
  }

  getMinute(): number {
    return Math.trunc(this.totalMinute % 60)
  }

  getDecimalHour(): number {
    return this.totalMinute / 60
  }

}

export class HourMinuteFormatException {
}
