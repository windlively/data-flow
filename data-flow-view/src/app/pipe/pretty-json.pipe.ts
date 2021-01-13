import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'prettyJSON'
})
export class PrettyJSONPipe implements PipeTransform {

  transform(value: Object, convertLineBreak?: boolean, space?: string): unknown {
    let s = JSON.stringify(value, null, space);
    if(convertLineBreak){
      s = s.replace(/\n/g, '<br>')
    }
    console.log(s);
    return s;
  }

}
