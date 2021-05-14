<template>
	<div class="djsdiv">
		{{h}}:{{m}}:{{s}}
	</div>
</template>

<script>
	var now;
	var begin;
	var timer;
	export default {
		name: "SecKillTime",
		props: {
			overTime: {
				type: String
			},
			overFunc: {
				type: Function
			}
		},
		data() {
			return {
				h: "00",
				m: "00",
				s: "00"
			}
		},
		methods: {
			djs() {
				var _this = this
				//什么时候结束
				var over = new Date(this.overTime);
				//当前时间
				// var now = new Date();
				//转换成毫秒值
				var end = over.getTime();
				//中间差距的时间，相差多少毫秒
				var times = end - begin;


				if (times <= 0) {

					this.overFunc();
					return;
				}


				var hour = parseInt(times / 1000 / 60 / 60);
				var min = parseInt(times / 1000 / 60 % 60);
				var sec = parseInt(times / 1000 % 60);

				this.h = this.formatTime(hour);
				this.m = this.formatTime(min);
				this.s = this.formatTime(sec);

				timer = setTimeout(function() {
					begin += 1000;
					_this.djs();
				}, 1000);

			},
			formatTime(s) {
				return s < 10 ? ("0" + s) : s;
			},
			getServerNow() {
				var _this = this;
				this.utils.ajax({
					url: this.utils.urls.now,
					success: function(data) {
						now = new Date(data);
						begin = now.getTime();
						_this.djs();
					}
				}, this);
			}
		},
		watch: {
			overTime(v) {
				this.getServerNow();
			}
		},
		mounted() {
			this.$once('hook:beforeDestroy', () => {
				clearTimeout(timer) // 此处的timer即 上文const的 timer
			})
		}
	}
</script>

<style scoped>
	.djsdiv {
		font-size: 30px;
		color: red;
	}
</style>
