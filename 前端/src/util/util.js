import Qs from 'qs'
export default {
	baseUrl : "localhost",
	urls:{
		getTimes:"http://localhost/goods/times",
		listByTime : "http://localhost/goods/listByTime",
		getGoodsById: "http://localhost/goods/getGoodsById",
		now:"http://localhost/goods/now",
		kill:"http://localhost/kill/qianggou"
		
	},
	ajax(p,vm){
		vm.axios({
			method:"POST",
			url:p.url,
			transformRequest : [function(data){
				return Qs.stringify(data)
			}],
			data:p.data?p.data:{}
		}).then(function(res){
			var result = res.data;
			if(result.code ==0){
				p.success(result.data);
			}else{
				vm.$message.error(result.msg)
			}
		});
	}
}