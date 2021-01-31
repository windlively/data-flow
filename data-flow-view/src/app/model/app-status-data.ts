export class AppStatusData {

  msgReceivedCount: object;

  msgProcessedCount: object;

  inflowCount: object;

  successfulCount: object;

  failureCount: object;

  historyReceivedCount: object;

  timestamp: number;

  static isEmpty = (statusData: AppStatusData): boolean => {
    return Object.keys(statusData.msgReceivedCount).length === 0
  }

}


