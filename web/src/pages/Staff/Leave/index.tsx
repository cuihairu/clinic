
import React from 'react';
import { createStaff } from '@/services/ant-design-pro/staff';
import {ProForm,  ProFormTextArea,ProFormDateRangePicker, ProFormSelect} from '@ant-design/pro-components'
import {message} from "antd";

type Leave ={
  leaveType?: number;
  reason?: string;
  startTime?: string;
  endTime?: string;
  createTime?: string;
  updateTime?: string;
  id?: number;
}

const Create: React.FC = () => {
  return (
      <ProForm<Leave>
        grid
        autoFocusFirstInput
        omitNil
        layout={'vertical'}
        onFinish={async (values) => {
          const result = await createStaff(values);
          console.log(result)
          if (result.id) {
            message.success("创建成功")
            return true;
          }
          return false;
        }
        }
      >
        <ProFormSelect colProps={{ md: 4, xl: 4 }} name="leaveType" label="类型" valueEnum={{0:'病假',1:'事假'}} placeholder="选择请假类型"/>
        <ProFormTextArea colProps={{ md:4,xl:8}} name="reason" label="原因" tooltip="长度范围2-20字符,必填项" placeholder="输入请假的理由" required={true}/>
        <ProFormDateRangePicker
          colProps={{ xl: 8, md: 12 }}
          label="时间范围"
        />
      </ProForm>);
};

export default Create;
