from pathlib import Path
path = Path('boot-app/src/main/resources/templates/fragments/sidebar-cliente.html')
text = path.read_text()
old = """      <li>
        <a th:href="@{/cliente/senha}"
           class="sidebar__link"
           th:classappend="${active}=='senha' ? ' is-active' : ''"
           th:attr="aria-current=\\=='senha' ? 'page' : null">Seguran? da conta</a>
      </li>
"""
new = """      <li>
        <a th:href="@{/cliente/senha}"
           class="sidebar__link"
           th:classappend="${active}=='senha' ? ' is-active' : ''"
           th:attr="aria-current=${active}=='senha' ? 'page' : null">Segurança da conta</a>
      </li>
"""
if old not in text:
    raise SystemExit('old block not found')
text = text.replace(old, new, 1)
path.write_text(text)
