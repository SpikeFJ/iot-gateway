export default {
    data() {
        return {
            online_list: []
        }
    },
    mounted() {
        this.getOnline();
    },
    methods: {
        getOnline() {
            fetch('/tcp/onlineList', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json;charset=utf-8'
                }
            })
                .then(resp => resp.json())
                .then(res => {
                    if (res.code != 1) {

                    } else {
                        res.data.forEach(element => {
                            var tmp = {};
                            tmp.connect_time = element.createTime;
                            tmp.remote_ip = element.remoteAddress;
                            tmp.receive_packets = element.receivedPackets;
                            tmp.send_packets = element.sendPackets;
                            tmp.receive_time = element.lastReadTime;
                            tmp.device_id = element.deviceId;
                            tmp.bussiness_id = element.bussinessId;

                            this.online_list.push(tmp);
                        });
                    }
                });
        }
    },
    template: `
    <div id="onlineList" class="overflow-auto h-2/3 m-5">
      <table class="table table-pin-rows table-pin-cols">
        <thead>
          <tr>
            <th class="text-center">序号</th>
            <th class="text-center">连接时间</th>
            <th class="text-center">远程IP端口</th>
            <th class="text-center">发送数据包</th>
            <th class="text-center">接收数据包</th>
            <th class="text-center">最后一次接收时间</th>
            <th class="text-center">唯一标识</th>
            <th class="text-center">业务id</th>
            <th class="text-center">实时</th>
            <th class="text-center">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(item, index) in online_list" :key="item.device_id">
            <th class="text-center">{{index+1}}</th>
            <td class="text-center">{{item.connect_time}}</td>
            <td class="text-center">{{item.remote_ip}}</td>
            <td class="text-center">{{item.receive_packets}}</td>
            <td class="text-center">{{item.send_packets}}</td>
            <td class="text-center">{{item.receive_time}}</td>
            <td class="text-center">{{item.device_id}}</td>
            <td class="text-center">{{item.bussiness_id}}</td>
            <td class="text-center">
              <input type="checkbox" class="toggle toggle-primary toggle-xs" v-model="item.notify"/>
            </td>
            <td class="text-center">
              <a class="link link-primary">详情</a>
            </td>
          </tr>
        </tbody>
  
      </table>
    </div> 
    `
}