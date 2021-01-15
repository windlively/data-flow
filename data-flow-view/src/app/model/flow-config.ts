
export interface FlowConfig{

  _id: string;

  source: string;

  schema: string;

  name: string;

  node_list: {

    node_name: string,

    resolver_order: string[],

    eval_context: {

      name: string,

      data_source: string,

      sql: string,

      type: string,

      on_condition: string,

      expression: string

    }[],

    skip_if_exception: boolean,

    filter: string,

    simple_copy_fields: boolean,

    simple_convert: Object,

    export_to_mp: {

      data: string,

      topic: string,

      mq_name: string,

      mq_type: string,

      tag: string

    },

    export_to_rdb: {

      method: string,

      target_data_source: string,

      target_schema: string,

      target_table: string,

      sql_log: boolean,

      data: string,

      find_original: {

        sql: string,

        match_fields: string[]

      },

      update: {

        sql: string,

        match_fields: string[],

        custom_fields: Object

      },

      insert: {

        sql: string,

        custom_fields: Object

      }

    }

    conditional_expression: ConditionalExpression[],

    additional_expression: string[]

  }[];

}

export class ConditionalExpression{

  condition: string;

  expression: string|ConditionalExpression[]

}
