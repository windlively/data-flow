export class Tools {
  public static groupBy = (collection: any[], classifier: (obj: any) => string): object => {
    const result = {};
    for (const item of collection) {
      const key: string = classifier(item);
      if (!!!result.hasOwnProperty(key)) {
        result[key] = [];
      }
      result[key].push(item);
    }
    return result;
  };
}
