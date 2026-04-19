<script setup>
import { onLaunch } from '@dcloudio/uni-app'

onLaunch(() => {
  console.log('智能手语翻译工具安卓端已启动')
})
</script>

<style lang="scss">
page {
  min-height: 100%;
  background:
    radial-gradient(circle at top, rgba(146, 185, 255, 0.35), transparent 38%),
    linear-gradient(180deg, #edf3ff 0%, #f8fbff 56%, #fff8ef 100%);
  color: var(--app-text-primary);
  font-family: "PingFang SC", "Microsoft YaHei", sans-serif;
}

view,
text,
button,
input,
textarea,
scroll-view {
  box-sizing: border-box;
}

button {
  margin: 0;
  padding: 0;
  background: transparent;
  line-height: 1;
}

button::after {
  border: 0;
}
</style>
