(async () => {
  const D = {"name": "SmartClean（智能清理）", "ename": "SmartClean", "sname": "", "intro": "分页回收站 + 按物品独立倒计时 + 物品压缩的服务端掉落物清理模组", "modid": "smartclean", "cfprojectid": "1825747", "mrprojectid": "", "core": "实用", "elements": ["优化"], "platforms": ["JAVA版 (JAVA Edition)"], "versions": {"Forge": ["1.20.1"], "NeoForge": ["1.21.1", "26.1.2"]}, "client": "需装", "server": "需装", "status": "活跃", "source": "开源", "tags": "服务器,清理,掉落物,回收站", "keys": "清理,掉落物清理,自动清理,回收站,扫地", "authors": [{"id": "41055", "name": "QLNPLUS", "roles": ["所有者"]}], "links": [["curseforge", "https://www.curseforge.com/minecraft/mc-mods/smart-clean", ""], ["github", "https://github.com/QLNPLUS/smart_clean", ""]], "content": "<p>[ban:title_menu]</p><p>[h1=简介]</p>\n<p><strong>SmartClean（智能清理）</strong>是一个面向服务端的掉落物清理与回收站模组。地上的掉落物不会被直接删除，而是被扫进一个共享回收站，玩家可以逐页浏览并把还需要的东西取回去；没被及时取走的物品按各自的倒计时清掉，具体留多久可以按物品类型配置。</p>\n<p>本模组基于 <strong>ClearDrops</strong>（作者 YuWan / YuWan-030）派生，属于非官方分支，与原作者的立场无关。</p>\n<p>[h1=特性]</p>\n<p>[h2=分页回收站]</p>\n<p>回收站每页 54 格，可翻多页。翻页箭头画在容器界面的左右两侧，不再用玻璃或占位物品当按钮，每页 54 格全部用于物品；<kbd>Esc</kbd> 照常关闭界面。</p>\n<p>[h2=按物品 ID 的世界侧倒计时]</p>\n<p>可以给任意物品 ID 单独设定在世界上存在的时间（例如圆石 180 秒后被扫走），到点后会按自己的计时优先回收，不必等全局清理周期。</p>\n<p>[h2=按物品 ID 的回收站保留倒计时]</p>\n<p>可以给任意物品 ID 单独设定在回收站里的保留时间（例如钻石留 600 秒、其它物品 120 秒）。每个槽位独立倒计时，并在槽位角上显示剩余时间；这类物品的保留时间优先级高于全局设置。</p>\n<p>[h2=物品压缩]</p>\n<p>某种物品在回收站里堆够数量后自动转换成另一种物品，例如 8 个原木换成 6 个木板。规则写作 <code>源ID,源数量,目标ID,目标数量</code>，每满一份就替换并可以反复触发；会导致无限增长的自反规则在加载时会被直接拒绝。</p>\n<p>[h2=其它]</p>\n<p>定时清理与逐档预警广播、物品黑名单、虚空物品保护、支持渐变标签与占位符的消息模板、配置热重载。</p>\n<p>[h1=命令]</p>\n<table class=\"table table-bordered text-nowrap\"><tbody>\n<tr><th style=\"word-break: break-all;\" align=\"center\">命令</th><th style=\"word-break: break-all;\" align=\"center\">权限</th><th style=\"word-break: break-all;\" align=\"center\">说明</th></tr>\n<tr><td style=\"word-break: break-all;\" align=\"center\"><code>/smartclean help</code></td><td style=\"word-break: break-all;\" align=\"center\">所有人</td><td style=\"word-break: break-all;\" align=\"center\">显示帮助</td></tr>\n<tr><td style=\"word-break: break-all;\" align=\"center\"><code>/smartclean gui</code></td><td style=\"word-break: break-all;\" align=\"center\">所有人</td><td style=\"word-break: break-all;\" align=\"center\">打开分页回收站</td></tr>\n<tr><td style=\"word-break: break-all;\" align=\"center\"><code>/smartclean status</code></td><td style=\"word-break: break-all;\" align=\"center\">所有人</td><td style=\"word-break: break-all;\" align=\"center\">查看槽位、物品数、页数与最近到期时间</td></tr>\n<tr><td style=\"word-break: break-all;\" align=\"center\"><code>/smartclean collect</code></td><td style=\"word-break: break-all;\" align=\"center\">等级 2</td><td style=\"word-break: break-all;\" align=\"center\">立刻清理世界掉落物</td></tr>\n<tr><td style=\"word-break: break-all;\" align=\"center\"><code>/smartclean purge</code></td><td style=\"word-break: break-all;\" align=\"center\">等级 2</td><td style=\"word-break: break-all;\" align=\"center\">立刻清空回收站（黑名单物品保留）</td></tr>\n<tr><td style=\"word-break: break-all;\" align=\"center\"><code>/smartclean reload</code></td><td style=\"word-break: break-all;\" align=\"center\">等级 2</td><td style=\"word-break: break-all;\" align=\"center\">重新应用服务端配置</td></tr>\n</tbody></table>\n<p>[h1=配置]</p>\n<p>首次启动时在 <code>&lt;存档目录&gt;/serverconfig/smartclean-server.toml</code> 生成配置文件，保存后自动热重载，也可以执行 <code>/smartclean reload</code>。主要配置项：<code>ticksPerCollect</code>（清理间隔）、<code>ticksKeepAfterCollect</code>（默认保留时长）、<code>maxPages</code>（回收站页数）、<code>blacklist.items</code>（黑名单）、<code>sweep_per_item.itemLifetimes</code>（世界侧独立倒计时）、<code>retention_per_item.itemKeepDurations</code>（回收站独立保留时长）、<code>compress.rules</code>（压缩规则）。</p>\n<p>[h1=兼容性]</p>\n<p>分别提供 Forge 1.20.1、NeoForge 1.21.1、NeoForge 26.1.2 的构建，请按服务端使用的加载器与游戏版本选择对应文件。翻页按钮与倒计时数字由客户端渲染，<strong>客户端也需要安装本模组</strong>；纯原版客户端无法打开分页回收站。</p>\n<p>[h1=注意事项]</p>\n<p>回收站是全局共享的，所有玩家看到同样的页面；同一服务端只应保留一个清理类模组，多个清理器同时运行会导致重复清扫；压缩规则只按物品 ID 匹配，不看 NBT 与数据组件。</p>\n<p>[h1=鸣谢与许可]</p>\n<p>回收站与清理设计来自 <strong>ClearDrops</strong>（作者 YuWan / YuWan-030，<a href=\"https://github.com/YuWan-030/cleardrops\">https://github.com/YuWan-030/cleardrops</a>），感谢原作者打下的基础。本项目以 CC BY 4.0 发布，源码位于 <a href=\"https://github.com/QLNPLUS/smart_clean\">https://github.com/QLNPLUS/smart_clean</a>。</p>", "images": {}, "cover": "", "injectCover": false, "coverPath": ""};
  const sleep = ms => new Promise(r => setTimeout(r, ms));
  const warn = [];
  const labelOf = el => {
    const l = document.querySelector(`label[for="${el.id}"]`);
    return (l ? l.innerText : el.parentElement.innerText).trim();
  };
  const setVal = (el, v) => {
    el.value = v;
    el.dispatchEvent(new Event('input', {bubbles: true}));
    el.dispatchEvent(new Event('change', {bubbles: true}));
  };
  const checkByLabel = (name, text) => {
    const el = [...document.querySelectorAll(`input[name="${name}"]`)].find(e => labelOf(e) === text);
    if (!el) { warn.push(`${name} 找不到选项：${text}`); return null; }
    if (!el.checked) el.click();
    return el;
  };
  const setTags = (id, value) => {
    if (!value) return;
    const el = document.getElementById(id);
    if (!el) { warn.push(`找不到 #${id}`); return; }
    if ($(el).data('tagsinput')) {
      $(el).tagsinput('removeAll');
      $(el).tagsinput('add', value);
    } else {
      setVal(el, value);
    }
    if (el.value !== value) warn.push(`#${id} 写入失败，当前值：${el.value}`);
  };

  setVal(document.querySelector('[data-multi-id=name]'), D.name);
  setVal(document.querySelector('[data-multi-id=ename]'), D.ename || '');
  if (D.sname) setVal(document.querySelector('[data-multi-id=sname]'), D.sname);

  for (const p of D.platforms) checkByLabel('class-platform', p);
  for (const loader of Object.keys(D.versions)) {
    const api = checkByLabel('class-api', loader);
    if (!api) continue;
    await sleep(800);
    const part = api.id.replace('class-data-api-', '');
    for (const v of D.versions[loader]) checkByLabel(`class-mcversion-${part}`, v);
  }

  const CORE = {'科技': '1,1', '魔法': '1,2', '冒险': '1,3', '农业': '1,4', '装饰': '1,5', '实用': '1,23', '辅助': '1,24', '魔改': '1,21', 'LIB': '1,7', '前置': '1,7'};
  const cats = [D.core, ...D.elements];
  for (const c of cats) {
    let a;
    if (/^\d+,\d+$/.test(c)) a = document.querySelector(`a[data-multi-id=category][data-multi-value="${c}"]`);
    else if (CORE[c] && c === D.core) a = document.querySelector(`a[data-multi-id=category][data-multi-value="${CORE[c]}"]`);
    else a = [...document.querySelectorAll('a[data-multi-id=category][data-category-type="1"]')].find(e => e.innerText.trim() === c);
    if (!a) { warn.push(`找不到模组元素：${c}`); continue; }
    if (a.dataset.categorySelected !== 'true') { a.click(); await sleep(150); }
  }

  for (const [head, href, text] of D.links) {
    document.querySelector('#link-frame .link-add').click();
    await sleep(400);
    const rows = document.querySelectorAll('#link-frame .link-row');
    const row = rows[rows.length - 1];
    const sel = row.querySelector('select.link-head-select');
    $(sel).selectpicker('val', head);
    $(sel).trigger('change');
    setVal(row.querySelector('.link-text-href'), href);
    setVal(row.querySelector('.link-text-text'), text || '');
  }

  setVal(document.getElementById('class-intro'), D.intro);
  setTags('class-modid', D.modid);
  setTags('class-cfprojectid', D.cfprojectid);
  setTags('class-mrprojectid', D.mrprojectid);
  setTags('class-tags', D.tags);
  setTags('class-keys', D.keys);
  checkByLabel('class-mode-1', D.client);
  checkByLabel('class-mode-2', D.server);
  checkByLabel('class-status', D.status);
  checkByLabel('class-source', D.source);

  for (const au of D.authors) {
    if (document.querySelector(`#edit-page-3 .author-row[data-id="${au.id}"], .author-list .author-row[data-id="${au.id}"]`)) continue;
    document.querySelector('.author-link').click();
    await sleep(300);
    const box = document.querySelector('.common-select-form .common-select');
    setVal(box.querySelector('.find-key'), String(au.id));
    box.querySelector('.find-btn').click();
    let tr = null;
    for (let i = 0; i < 30 && !tr; i++) { await sleep(200); tr = box.querySelector(`tr[data-id="${au.id}"]`); }
    if (!tr) { warn.push(`搜索不到作者 ${au.name}（ID ${au.id}）`); continue; }
    tr.click();
    await sleep(500);
    const row = document.querySelector(`.author-list .author-row[data-id="${au.id}"]`);
    if (!row) { warn.push(`作者 ${au.name} 添加失败`); continue; }
    if (au.roles && au.roles.length) {
      const sel = row.querySelector('select.author-head-select');
      $(sel).selectpicker('val', au.roles);
      $(sel).trigger('change');
    }
  }

  // 正文里的 {{img:键}} 先通过编辑器的图片上传接口传到百科，再替换成 <img>
  let content = D.content;
  for (const [key, img] of Object.entries(D.images || {})) {
    const token = `{{img:${key}}}`;
    let tag = `【请手动插入图片：${img.file}】`;
    try {
      const action = editor.getActionUrl(editor.getOpt('imageActionName'));
      const fd = new FormData();
      const bin = Uint8Array.from(atob(img.data), c => c.charCodeAt(0));
      fd.append(editor.getOpt('imageFieldName') || 'upfile', new File([bin], img.file, {type: 'image/png'}));
      const res = await (await fetch(action, {method: 'POST', body: fd, credentials: 'include'})).json();
      if (res.state === 'SUCCESS') tag = `<img src="${(editor.getOpt('imageUrlPrefix') || '') + res.url}" alt="${img.file}"/>`;
      else warn.push(`图片 ${img.file} 上传失败：${res.state}`);
    } catch (e) {
      warn.push(`图片 ${img.file} 上传失败：${e}`);
    }
    content = content.split(token).join(tag);
    await sleep(500);
  }
  editor.setContent(content);

  if (D.cover && D.injectCover) {
    const bin = Uint8Array.from(atob(D.cover), c => c.charCodeAt(0));
    const dt = new DataTransfer();
    dt.items.add(new File([bin], 'cover.png', {type: 'image/png'}));
    const input = document.getElementById('cover-select-button');
    input.files = dt.files;
    input.dispatchEvent(new Event('change', {bubbles: true}));
    await sleep(1500);
    // 注入后页面会弹出裁剪框，点「更新封面」把裁剪结果写回表单
    const update = document.getElementById('cover-update-button');
    if (update && !update.classList.contains('hidden')) {
      update.click();
      await sleep(1500);
      const preview = document.getElementById('class-cover-preview-img');
      const src = preview ? preview.src : '';
      if (/^data:image/.test(src)) console.log('%c封面已注入并确认裁剪', 'color:#0a0;font-weight:bold');
      else warn.push('封面注入后没确认成功，请手动点「更新封面」');
    } else {
      warn.push('封面注入后没等到「更新封面」按钮，请手动处理裁剪框');
    }
  }

  const check = {
    名称: document.querySelector('[data-multi-id=name]').value,
    简介: document.getElementById('class-intro').value,
    MODID: document.getElementById('class-modid').value,
    CurseForge: document.getElementById('class-cfprojectid').value,
    Modrinth: document.getElementById('class-mrprojectid').value,
    作者: [...document.querySelectorAll('.author-list .author-row')].map(r => (r.querySelector('a b') || r).innerText.trim()).join(' / '),
  };
  console.table(check);
  if (!check.MODID) warn.push('MODID 为空，必须手动补填');
  if (warn.length) console.warn('需要手动处理：\n' + warn.join('\n'));
  console.log('%c填写完成。' + (D.injectCover ? '封面已自动注入并确认裁剪；' : '封面请点「上传封面」手动选择：' + D.coverPath + '；') + '检查三页内容后自己点「完成添加」。', 'color:#0a0;font-size:14px');
})();
