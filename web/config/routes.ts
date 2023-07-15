/**
 * @name umi 的路由配置
 * @description 只支持 path,component,routes,redirect,wrappers,name,icon 的配置
 * @param path  path 只支持两种占位符配置，第一种是动态参数 :id 的形式，第二种是 * 通配符，通配符只能出现路由字符串的最后。
 * @param component 配置 location 和 path 匹配后用于渲染的 React 组件路径。可以是绝对路径，也可以是相对路径，如果是相对路径，会从 src/pages 开始找起。
 * @param routes 配置子路由，通常在需要为多个路径增加 layout 组件时使用。
 * @param redirect 配置路由跳转
 * @param wrappers 配置路由组件的包装组件，通过包装组件可以为当前的路由组件组合进更多的功能。 比如，可以用于路由级别的权限校验
 * @param name 配置路由的标题，默认读取国际化文件 menu.ts 中 menu.xxxx 的值，如配置 name 为 login，则读取 menu.ts 中 menu.login 的取值作为标题
 * @param icon 配置路由的图标，取值参考 https://ant.design/components/icon-cn， 注意去除风格后缀和大小写，如想要配置图标为 <StepBackwardOutlined /> 则取值应为 stepBackward 或 StepBackward，如想要配置图标为 <UserOutlined /> 则取值应为 user 或者 User
 * @doc https://umijs.org/docs/guides/routes
 */
export default [
  {
    path: '/dash',
    icon: 'DashboardOutlined',
    name: 'dashboard',
    routes: [
      {
        name: 'day',
        path: '/dash/day',
        component: './Dashboard/Day',
      },
    ],
  },
  {
    path: '/user',
    layout: false,
    routes: [
      {
        name: 'login',
        path: '/user/login',
        component: './User/Login',
      },
    ],
  },
  {
    path: '/customer',
    name: 'customer',
    icon: 'smile',
    routes: [
      {
        name: 'customer-create',
        path: '/customer/create',
        component: './Customer/Create',
      },
      {
        name: 'customer-query',
        path: '/customer/query',
        component: './Customer/Query',
      },
      {
        path: '/customer/update',
        component: './Customer/Update',
      },
    ]
  },
  {
    path: '/staff',
    name: 'staff',
    icon: 'user',
    routes: [
      {
        access: 'canAdmin',
        name: 'staff-create',
        path: '/staff/create',
        component: './Staff/Create',
      },
      {
        access: 'canAdmin',
        name: 'staff-query',
        path: '/staff/query',
        component: './Staff/Query',
      },
      {
        path: '/staff/update',
        component: './Staff/Update',
      },
      {
        access: 'canUser',
        name: 'staff-sign',
        path: '/staff/sign',
        component: './Staff/Sign',
      },
      {
        access: 'canUser',
        name: 'staff-today-timesheet',
        path: '/staff/sign/timesheet/today',
        component: './Staff/Timesheet/Today',
      },
      {
        path: '/staff/leave',
        component: './Staff/Leave',
      },
      {
        name: 'timesheet-list',
        path: '/staff/timesheet/list',
        component: './Staff/Timesheet/List',
      },
    ]
  },
  {
    path: '/',
    redirect: '/dash/day',
  },
  {
    path: '/treat/',
    icon: 'table',
    name: 'treat',
    routes:[
      {
        path: '/treat/create',
        component: './Treat/Create',
      },
      {
        name: 'treat-query',
        path: '/treat/query',
        component: './Treat/Query',
      },
      {
        path: '/treat/history',
        component: './Treat/History',
      }
    ],
  },
  {
    path: '/item/',
    icon: 'FileDoneOutlined',
    name: 'item',
    routes:[
      {
        name: 'item-create',
        path: '/item/create',
        component: './Item/Create',
      },
      {
        name: 'item-query',
        path: '/item/query',
        component: './Item/Query',
      },
      {
        path: '/item/spread',
        component: './Item/Spread',
      },
      {
        path: '/item/update',
        component: './Item/Update',
      },
    ],
  },
  {
    path: '/order/',
    icon: 'AccountBookOutlined',
    name: 'order',
    routes:[
      {
        path: '/order/create',
        component: './Order/Create',
      },
      {
        path: '/order/query',
        component: './Order/Query',
      }
    ],
  },
  {
    path: '/review/',
    icon: 'WhatsAppOutlined',
    name: 'review',
    routes:[
      {
        path: '/review/staff',
        name: 'staff',
        component: './Review/Staff',
      },
      {
        path: '/review/customer',
        name:'customer',
        component: './Review/Customer',
      },
      {
        path: '/review/day',
        name: 'day',
        component: './Review/Day',
      },
      {
        path: '/review/plan',
        component: './Review/Plan',
      }
    ],
  },
  {
    path: '*',
    layout: false,
    component: './404',
  },
];
