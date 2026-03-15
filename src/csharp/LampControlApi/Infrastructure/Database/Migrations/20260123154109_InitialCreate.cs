using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace LampControlApi.Infrastructure.Database.Migrations
{
    /// <inheritdoc />
    public partial class InitialCreate : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.CreateTable(
                name: "lamps",
                columns: table => new
                {
                    id = table.Column<Guid>(type: "uuid", nullable: false),
                    is_on = table.Column<bool>(type: "boolean", nullable: false),
                    created_at = table.Column<DateTimeOffset>(type: "timestamp with time zone", nullable: false),
                    updated_at = table.Column<DateTimeOffset>(type: "timestamp with time zone", nullable: false),
                    deleted_at = table.Column<DateTimeOffset>(type: "timestamp with time zone", nullable: true)
                },
                constraints: table =>
                {
                    table.PrimaryKey("PK_lamps", x => x.id);
                });

#pragma warning disable CA1861
            migrationBuilder.CreateIndex(
                name: "idx_lamps_active_created_at_id",
                table: "lamps",
                columns: new[] { "created_at", "id" },
                filter: "deleted_at IS NULL");
#pragma warning restore CA1861

            migrationBuilder.CreateIndex(
                name: "idx_lamps_active_is_on",
                table: "lamps",
                column: "is_on",
                filter: "deleted_at IS NULL");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropTable(
                name: "lamps");
        }
    }
}
